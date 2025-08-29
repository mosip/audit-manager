#!/bin/bash
# Installs all auditmanager helm charts 
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=kernel
CHART_VERSION=0.0.1-develop

echo Create $NS namespace
kubectl create ns $NS

function installing_auditmanager() {
  echo Istio label
  kubectl label ns $NS istio-injection=enabled --overwrite
  helm repo update

  echo Copy configmaps
  sed -i 's/\r$//' copy_cm.sh
  ./copy_cm.sh

  ENABLE_INSECURE=''
  if [ ! -z "${ENABLE_SSL_INSECURE:-}" ]; then
    if [ "$ENABLE_SSL_INSECURE" = "true" ]; then
      ENABLE_INSECURE='--set enable_insecure=true'
    fi
  else
    # Interactive mode
    echo "Do you have public domain & valid SSL? (Y/n) "
    echo "Y: if you have public domain & valid ssl certificate"
    echo "n: If you don't have a public domain and a valid SSL certificate. Note: It is recommended to use this option only in development environments."
    read -p "" flag

    if [ -z "$flag" ]; then
      echo "'flag' was NOT provided; EXITING;"
      exit 1;
    fi
    
    if [ "$flag" = "n" ]; then
      ENABLE_INSECURE='--set enable_insecure=true';
    fi
  fi

  PERSISTENT_STORAGE=''
  VOLUME_PERMISSIONS=''
  
  if [ ! -z "${ENABLE_PERSISTENT_STORAGE:-}" ]; then
    if [ "$ENABLE_PERSISTENT_STORAGE" = "true" ]; then
      PERSISTENT_STORAGE='--set auditLogsPersistence.enabled=true'
      
      if [ ! -z "${STORAGE_SIZE:-}" ]; then
        PERSISTENT_STORAGE="$PERSISTENT_STORAGE --set auditLogsPersistence.size=$STORAGE_SIZE"
      fi
      
      if [ ! -z "${STORAGE_CLASS:-}" ]; then
        PERSISTENT_STORAGE="$PERSISTENT_STORAGE --set auditLogsPersistence.storageClass=$STORAGE_CLASS"
      fi
      
      if [ "${ENABLE_VOLUME_PERMISSIONS:-}" = "true" ]; then
        VOLUME_PERMISSIONS='--set volumePermissions.enabled=true'
      fi
    fi
  else
    echo ""
    echo "=== Audit Logs Persistent Storage Configuration ==="
    echo "Do you want to enable persistent storage for audit logs (including WAL files)? (Y/n)"
    echo "Y: Enable persistent storage"
    echo "n: Use ephemeral storage "
    read -p "" persistent_flag

    if [ "$persistent_flag" != "n" ]; then
      echo ""
      echo "Enabling persistent storage for audit logs..."
      PERSISTENT_STORAGE='--set auditLogsPersistence.enabled=true'
      
      echo ""
      echo "Enter storage size for audit logs (default: 1Gi): "
      read -p "" storage_size
      if [ ! -z "$storage_size" ]; then
        PERSISTENT_STORAGE="$PERSISTENT_STORAGE --set auditLogsPersistence.size=$storage_size"
      fi
      
      echo ""
      echo "Enter storage class name (leave empty for default): "
      read -p "" storage_class
      if [ ! -z "$storage_class" ]; then
        PERSISTENT_STORAGE="$PERSISTENT_STORAGE --set auditLogsPersistence.storageClass=$storage_class"
      fi
      
      echo ""
      echo "Do you want to enable volume permissions init container? (Y/n)"
      echo "Y: Enable init container to set proper permissions"
      echo "n: Skip permissions init container"
      read -p "" permissions_flag
      
      if [ "$permissions_flag" != "n" ]; then
        VOLUME_PERMISSIONS='--set volumePermissions.enabled=true'
      fi
    fi
  fi

  echo ""
  echo "Installing auditmanager with the following configuration:"
  echo "- SSL Configuration: $ENABLE_INSECURE"
  echo "- Persistent Storage: $PERSISTENT_STORAGE"
  echo "- Volume Permissions: $VOLUME_PERMISSIONS" 
  echo ""

  # Install auditmanager using helm
  helm -n $NS install auditmanager mosip/auditmanager --version $CHART_VERSION \
    $ENABLE_INSECURE \
    $PERSISTENT_STORAGE \
    $VOLUME_PERMISSIONS

  kubectl -n $NS  get deploy -o name |  xargs -n1 -t  kubectl -n $NS rollout status
  echo "Auditmanager successfully installed"
  return 0
}

# set commands for error handling.
set -e
set -o errexit   ## set -e : exit the script if any statement returns a non-true return value
set -o nounset   ## set -u : exit the script if you try to use an uninitialised variable
set -o errtrace  # trace ERR through 'time command' and other functions
set -o pipefail  # trace ERR through pipes
installing_auditmanager   # calling function
