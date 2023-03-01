#FaceRecognizer
---Projeto em desenvolvimento---

#Projeto FaceRecognizer Tem como finalidade fornecer API para reconhecimento de faces utilizando VIola-Jones com EIgenfaces, Fisherfaces e LbpH

Pré-requisitos
JDK 1.8

MAVEN 3.5

##Execução Para executar o projeto, utilize uma IDE (Eclipse ou Intellij) ou eecute o comando mvn spring-boot:run

#Ferramentas utilizadas

[Spring boot] - O framework web usado

[Maven] - Gerente de Dependência

[JavaCV] (https://github.com/bytedeco/javacv) - Wrapper para biblioteca do OpenCv

[Postman] (https://www.postman.com/) - Plataforma de API para desenvolvedores projetar, construir, testar e iterar suas APIs.

##Como usar

Segue cURl que pode ser importado no Postman

1ª Etapa: treinamento de faces:
Selecionar imagens com uma facce para treinamento

```cURL
curl --location 'http://localhost:8080/api/clientes/traine-register' \
--form 'images=@"/path/to/file"' \
--form 'email="EMAIL@ALUNO"' \
--form 'name="Aluno Nome"'
```

2ª Etapa: identificar face:
Selecione uma foto com a face registrada no treinamento para ser reconhecida
```cURL
curl --location 'http://localhost:8080/api/clientes/recognize' \
--form 'images=@"/path/to/file"'
```
