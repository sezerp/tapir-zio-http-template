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