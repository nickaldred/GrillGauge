# Generate CA

## Add Configuration: ~/rootCA/openssl.cnf

```bash
[ ca ]
default_ca = CA_default

[ CA_default ]
dir            = ~/rootCA
certs          = $dir/certs
crl_dir        = $dir/crl
new_certs_dir  = $dir/newcerts
database       = $dir/index.txt
serial         = $dir/serial

private_key    = $dir/private/GrillGauge-RootCA.key.pem
certificate    = $dir/certs/GrillGauge-RootCA.crt.pem

default_days   = 3650
default_md     = sha256

policy         = policy_strict

[ policy_strict ]
countryName             = match
stateOrProvinceName     = match
organizationName        = match
commonName              = supplied

[ req ]
default_bits        = 4096
encrypt_key         = yes
default_md          = sha256
prompt              = no
distinguished_name  = req_distinguished_name
x509_extensions     = v3_ca

[ req_distinguished_name ]
countryName                     = GB
stateOrProvinceName             = England
localityName                    = Birmingham
organizationName                = GrillGauge Security
commonName                      = GrillGauge Root CA

[ v3_ca ]
basicConstraints = critical,CA:TRUE
keyUsage = critical, digitalSignature, cRLSign, keyCertSign
subjectKeyIdentifier = hash
```

## Generate the private key

openssl genrsa -aes256 -out ~/rootCA/private/GrillGauge-RootCA.key.pem 4096
chmod 600 ~/rootCA/private/GrillGauge-RootCA.key.pem

## Generate the Root CA certificate

openssl req -config ~/rootCA/openssl.cnf \
    -key ~/rootCA/private/GrillGauge-RootCA.key.pem \
    -new -x509 -days 3650 -sha256 \
    -out ~/rootCA/certs/GrillGauge-RootCA.crt.pem

## Verify it

openssl x509 -noout -text -in ~/rootCA/certs/GrillGauge-RootCA.crt.pem
