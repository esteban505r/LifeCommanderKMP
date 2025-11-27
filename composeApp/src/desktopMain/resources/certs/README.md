# Cloudflare Certificate

Place your Cloudflare origin certificate here as `cloudflare-cert.pem`.

## How to get the certificate:

1. Go to Cloudflare Dashboard → SSL/TLS → Origin Server
2. Create or download your origin certificate
3. Copy the certificate content (the `.pem` file, not the key)
4. Save it as `cloudflare-cert.pem` in this directory

## File structure:

```
composeApp/src/desktopMain/resources/certs/
└── cloudflare-cert.pem  (your Cloudflare origin certificate)
```

## Note:

- Only the certificate is needed (not the private key)
- The certificate will be loaded from resources at runtime
- If the certificate is not found, the app will use the default system trust manager (which may show certificate warnings)

