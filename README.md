# sidebar-backend — Jasypt Encryption Guide (Local-oriented application.yml)

This service uses Jasypt to decrypt secrets when running locally. The current `application.yml` is effectively used for local, so no separate `application-test.yml` is needed.

## What’s included
- Jasypt starter dependency: `com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5`
- `application.yml` expects secrets via environment variables that contain `ENC(...)` ciphertexts
- Local fail-fast guard: exits if the master key is missing in local
- Test-based utility to generate encrypted strings
- Test verifying `ENC(...)` is injected as plaintext under `local`

## 1) Provide the master key (local only)
The master key must be provided via environment variable.

```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
```

## 2) Prepare encrypted values (ENC(...))
Use the dedicated test to print an `ENC(...)` value that matches runtime configuration exactly.

```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
./gradlew test \
  -Dspring.profiles.active=local \
  -Djasypt.value='your-secret-value' \
  --tests '*JasyptEncryptorTest.printEncrypted'
```

Copy the printed `ENC(...)` and provide it via environment variables:

```bash
# GitLab
export GITLAB_ACCESS_TOKEN_ENC='ENC(<ciphertext>)'
export GITLAB_ROOT_GROUP_ID_ENC='ENC(<ciphertext>)'               # if you also choose to encrypt it

# Webhook
export WEBHOOK_SECRET_TOKEN_ENC='ENC(<ciphertext>)'
```

`application.yml` is wired to read these variables and Jasypt will decrypt them at runtime:

```yaml
gitlab:
  access-token: ${GITLAB_ACCESS_TOKEN_ENC:}
  root-group-id: ${GITLAB_ROOT_GROUP_ID_ENC:}

webhook:
  secret-token: ${WEBHOOK_SECRET_TOKEN_ENC:}

jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD:}
```

## 3) Run locally
```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
./gradlew bootRun --args='--spring.profiles.active=local'
# or
SPRING_PROFILES_ACTIVE=local java -jar build/libs/*-SNAPSHOT.jar
```

If the key is missing, the app will exit immediately with a friendly message.

## Notes
- No `application-test.yml` is necessary.
- Do not commit plaintext secrets. Provide `ENC(...)` via environment variables instead.
- The default algorithm is `PBEWITHHMACSHA512ANDAES_256`.
