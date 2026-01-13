# Files Service — DigitalOcean Spaces Setup

This guide explains how to use DigitalOcean Spaces (S3 API) for the files-service.

## 1) Prerequisites

- Space name: lcdi-storage
- Region: tor1
- Access Key and Secret Key with write permissions to the Space
- Optional CDN: can be enabled from the Space settings

## 2) Environment variables

Set these in your .env (values are examples):

- SPACES_ENDPOINT=tor1.digitaloceanspaces.com
- SPACES_BUCKET=lcdi-storage
- SPACES_REGION=tor1
- SPACES_ACCESS_KEY=DO00EXAMPLEKEY
- SPACES_SECRET_KEY=DO00EXAMPLESECRET
- FILES_DB_URL=postgres://USER:PASSWORD@files-db:5432/DBNAME?sslmode=disable

docker-compose maps these to the service’s existing MINIO_* variables and enables SSL.

Notes:
- Do not include a protocol in SPACES_ENDPOINT (no https://).
- Use the region endpoint (tor1.digitaloceanspaces.com), not a bucket host.
- The service will address the bucket via the S3 API using the bucket name.

## 3) Starting the stack

- docker compose up --build

The files-service should connect to Spaces using the provided credentials.

## 4) CORS (if clients upload directly to Spaces)

If clients or signed URLs interact with the Space from browsers, configure CORS in the Space.
Example S3 CORS (XML) to allow common methods:

<CORSConfiguration>
  <CORSRule>
    <AllowedOrigin>*</AllowedOrigin>
    <AllowedMethod>GET</AllowedMethod>
    <AllowedMethod>PUT</AllowedMethod>
    <AllowedMethod>POST</AllowedMethod>
    <AllowedMethod>DELETE</AllowedMethod>
    <AllowedHeader>*</AllowedHeader>
    <ExposeHeader>ETag</ExposeHeader>
    <MaxAgeSeconds>3000</MaxAgeSeconds>
  </CORSRule>
</CORSConfiguration>

Tighten AllowedOrigin and methods as needed.

## 5) Public read policy (optional)

If you want objects to be publicly readable directly via the Spaces CDN/endpoint, add a bucket policy like:

{
  "Version":"2012-10-17",
  "Statement":[
    {
      "Sid":"PublicRead",
      "Effect":"Allow",
      "Principal":"*",
      "Action":["s3:GetObject"],
      "Resource":["arn:aws:s3:::lcdi-storage/*"]
    }
  ]
}

If you prefer private objects, omit this policy and serve files via signed URLs or through the files-service.

## 6) Local development with MinIO (optional)

You can still use MinIO locally:

- Start MinIO with a profile: docker compose --profile local-minio up -d minio
- Point envs to MinIO:
  - SPACES_ENDPOINT=minio:9000
  - SPACES_BUCKET=local-bucket
  - SPACES_REGION=us-east-1
  - SPACES_ACCESS_KEY=$MINIO_ROOT_USER
  - SPACES_SECRET_KEY=$MINIO_ROOT_PASSWORD

Then run docker compose up to start the files-service against the local MinIO.

## 7) Troubleshooting

- 403 or SignatureDoesNotMatch: confirm region and endpoint match (tor1 + tor1.digitaloceanspaces.com).
- TLS/handshake issues: ensure MINIO_USE_SSL=true is set (docker-compose does this by default).
- Bucket not found: verify SPACES_BUCKET exists in tor1 and names match exactly.
