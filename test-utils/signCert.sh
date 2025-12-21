#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <HUB_ID>"
    exit 1
fi

HUB_ID=$1
URL="http://localhost:8080/api/v1/register/$HUB_ID/csr"
KEY_FILE="hub_private_key.key"
CSR_FILE="hub_request.csr"
CERT_FILE="hub_signed_cert.crt"

echo "1. Generating Private Key and CSR..."

# Generate a 2048-bit RSA private key
openssl genrsa -out "$KEY_FILE" 2048

# Generate the CSR
openssl req -new -key "$KEY_FILE" -out "$CSR_FILE" \
    -subj "/C=UK/ST=State/L=City/O=GrillGauge/OU=Devices/CN=Hub-$HUB_ID"

echo "2. Sending CSR to server for signing..."

# Send the CSR PEM content as the raw request body
response=$(curl -s -X POST "$URL" \
     -H "Content-Type: text/plain" \
     --data-binary @"$CSR_FILE")

echo "--------------------------"
if [[ "$response" == *"BEGIN CERTIFICATE"* ]]; then
    echo "3. Success! Saving signed certificate to $CERT_FILE"
    echo "$response" > "$CERT_FILE"
    
    echo "--------------------------"
    echo "Certificate Details:"
    openssl x509 -in "$CERT_FILE" -text -noout | grep "Subject:\|Not After"
    echo "--------------------------"
else
    echo "Error: Server did not return a valid certificate."
    echo "Response: $response"
    exit 1
fi
echo "Done."