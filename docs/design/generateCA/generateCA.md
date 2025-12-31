# Generate Root and Intermediate CAs

This follows a more professional PKI layout:

- A **Root CA** whose private key stays offline and is used only to sign
    intermediate CAs.
- An **Intermediate CA** whose key is used in day‑to‑day operations to
    issue server certificates.

If the intermediate is ever compromised, you can revoke and replace it
with the Root CA, without distrusting the Root itself.

---

## Root CA

### Add configuration: `~/rootCA/openssl.cnf`

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

### Generate the Root CA private key

```bash
mkdir -p ~/rootCA/{certs,crl,newcerts,private}
chmod 700 ~/rootCA/private
touch ~/rootCA/index.txt
echo 1000 > ~/rootCA/serial

openssl genrsa -aes256 -out ~/rootCA/private/GrillGauge-RootCA.key.pem 4096
chmod 600 ~/rootCA/private/GrillGauge-RootCA.key.pem
```

### Generate the Root CA certificate

```bash
openssl req -config ~/rootCA/openssl.cnf \
        -key ~/rootCA/private/GrillGauge-RootCA.key.pem \
        -new -x509 -days 3650 -sha256 \
        -out ~/rootCA/certs/GrillGauge-RootCA.crt.pem
```

### Verify the Root CA

```bash
openssl x509 -noout -text -in ~/rootCA/certs/GrillGauge-RootCA.crt.pem
```

Keep the Root CA key and machine **offline** in normal operation.

---

## Intermediate CA

The intermediate CA is what issues certificates for GrillGauge hubs,
probes, and services.

### Create the intermediate CA directory structure

```bash
mkdir -p ~/intermediateCA/{certs,crl,csr,newcerts,private}
chmod 700 ~/intermediateCA/private
touch ~/intermediateCA/index.txt
echo 1000 > ~/intermediateCA/serial
echo 1000 > ~/intermediateCA/crlnumber
```

### Add configuration: `~/intermediateCA/openssl.cnf`

```bash
[ ca ]
default_ca = CA_default

[ CA_default ]
dir            = ~/intermediateCA
certs          = $dir/certs
crl_dir        = $dir/crl
new_certs_dir  = $dir/newcerts
database       = $dir/index.txt
serial         = $dir/serial
crlnumber      = $dir/crlnumber

private_key    = $dir/private/GrillGauge-Intermediate.key.pem
certificate    = $dir/certs/GrillGauge-Intermediate.crt.pem

default_days   = 1825
default_md     = sha256
policy         = policy_loose

[ policy_loose ]
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
x509_extensions     = v3_intermediate_ca

[ req_distinguished_name ]
countryName                     = GB
stateOrProvinceName             = England
localityName                    = Birmingham
organizationName                = GrillGauge Security
commonName                      = GrillGauge Intermediate CA

[ v3_intermediate_ca ]
basicConstraints = critical,CA:TRUE,pathlen:0
keyUsage = critical, digitalSignature, cRLSign, keyCertSign
authorityKeyIdentifier = keyid:always,issuer
subjectKeyIdentifier = hash

[ server_cert ]
basicConstraints = CA:FALSE
nsCertType = server
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
```

### Generate the intermediate CA private key

```bash
openssl genrsa -aes256 \
    -out ~/intermediateCA/private/GrillGauge-Intermediate.key.pem 4096
chmod 600 ~/intermediateCA/private/GrillGauge-Intermediate.key.pem
```

### Generate the intermediate CA CSR

```bash
openssl req -config ~/intermediateCA/openssl.cnf \
    -new -sha256 \
    -key ~/intermediateCA/private/GrillGauge-Intermediate.key.pem \
    -out ~/intermediateCA/csr/GrillGauge-Intermediate.csr.pem
```

### Sign the intermediate CA with the Root CA (offline machine)

On the offline Root CA machine:

```bash
openssl ca -config ~/rootCA/openssl.cnf \
    -extensions v3_ca \
    -days 1825 -notext -md sha256 \
    -in  ~/intermediateCA/csr/GrillGauge-Intermediate.csr.pem \
    -out ~/intermediateCA/certs/GrillGauge-Intermediate.crt.pem

chmod 644 ~/intermediateCA/certs/GrillGauge-Intermediate.crt.pem
```

Optionally, define a dedicated `v3_intermediate_ca` section in the Root
CA config and use that instead of `v3_ca`.

### Build the intermediate chain

```bash
cat ~/intermediateCA/certs/GrillGauge-Intermediate.crt.pem \
        ~/rootCA/certs/GrillGauge-RootCA.crt.pem \
    > ~/intermediateCA/certs/GrillGauge-Intermediate-chain.crt.pem
```

This chain file is what servers typically present (along with their own
leaf certificate).

---

## Issuing server certificates (from the Intermediate CA)

For each GrillGauge service or device:

1. Generate a private key and CSR on that host (or in your deployment
     pipeline), including the appropriate SANs.
2. Use **only the Intermediate CA** to sign the CSR.

Example (server side, using the `server_cert` extension above):

```bash
openssl ca -config ~/intermediateCA/openssl.cnf \
    -extensions server_cert \
    -days 825 -notext -md sha256 \
    -in server.csr.pem \
    -out server.crt.pem
```

Distribute:

- `server.key.pem` and `server.crt.pem` to the service/device.
- `GrillGauge-Intermediate-chain.crt.pem` as the chain presented by the
    server.
- `GrillGauge-RootCA.crt.pem` as the trust anchor installed on clients
    that need to trust GrillGauge devices.

The Root CA key never leaves the offline environment or participates in
day‑to‑day certificate issuance.
