# Tapir ZIO http template

Template containing simple Http application with tests that can be use as project boilerplate.
The project is inspired by [Softwaremill Bootzooka project](https://github.com/softwaremill/bootzooka)

### Stack
- ZIO
- ZIO logging
- Tapir
- Circe
- Http4s

## Getting started

Import project to intellij and run Application.

```shell
curl --location --request GET 'http://localhost:8080/api/v1/hello?name=Paweł'
```

With correlation ID to test if provided will appear in logs

```shell
curl --location --request GET 'http://localhost:8080/api/v1/hello?name=Paweł' \
--header 'X-Correlation-ID: test-correlation-id'
```

### Authentication

To signing JWT is used RS256, in resources are working example of public/private keys that CANNOT be use on production!

How to generate own keys:
```shell
ssh-keygen -t rsa -b 4096 -m PEM -f jwtRS256.key
openssl rsa -in jwtRS256.key -pubout -outform PEM -out jwtRS256.key.pub
```

[OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#work-factors)