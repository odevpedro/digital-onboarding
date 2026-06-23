#!/bin/sh
until mc ls local/ > /dev/null 2>&1; do
  echo "Waiting for MinIO..."
  sleep 2
done
mc mb local/onboarding-documentos --ignore-existing
mc anonymous set download local/onboarding-documentos
echo "MinIO initialized: bucket 'onboarding-documentos' ready"
