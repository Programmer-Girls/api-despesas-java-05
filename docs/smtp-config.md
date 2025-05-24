# Configuração SMTP

Este arquivo detalha as configurações necessárias para os servidores SMTP usados pela API Controle de Despesas para envio de emails, tanto em produção quanto em ambiente de testes.

---

## Servidores SMTP Utilizados

### Produção - Gmail

- Servidor SMTP: `smtp.gmail.com`
- Porta: `587`
- Usuário: seu-email@gmail.com
- Senha: senha do aplicativo gerada no Gmail (não use senha da conta diretamente)

> **Importante:** Para usar o Gmail como servidor SMTP, configure uma senha específica para aplicativo nas configurações da sua conta Google.

---

### 🚧 Pré-requisitos

Antes de tudo, para usar o servidor SMTP do Gmail em produção, você precisa:

- Habilitar o acesso SMTP na sua conta Google.
- Ativar a **verificação em 2 etapas** na sua conta Google.
- Criar uma **senha de app** para o SMTP, acessando: [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)

- Use essa senha gerada no campo `spring.mail.password` do arquivo `application.properties`.
- A senha gerada pelo Google geralmente vem com espaços, por exemplo: `abcd efgh ijkl mnop`

- No arquivo `application.properties`, **remova os espaços da senha** antes de usá-la. Exemplo:

```properties
spring.mail.password=abcdefghijklmnop
```

---

### Testes - Mailtrap

- Servidor SMTP: `sandbox.smtp.mailtrap.io`
- Porta: `2525`
- Usuário e senha: fornecidos pelo Mailtrap (na sua conta)

Para o Mailtrap (teste), basta usar as credenciais fornecidas na sua conta Mailtrap.
Acesse: [https://mailtrap.io/home](https://mailtrap.io/home)

---

### `application-test.properties` (testes)

```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=${SMTP_TEST_USERNAME}
spring.mail.password=${SMTP_TEST_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
---

## Configuração dos arquivos `application.properties`

### `application.properties` (produção)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SMTP_PROD_USERNAME}
spring.mail.password=${SMTP_PROD_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
