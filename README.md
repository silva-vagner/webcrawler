# Webcrawler


Esta é uma aplicação Java que permite aos usuários navegar por um website em busca de um termo fornecido e listar as URLs onde o termo foi encontrado. A interação com a aplicação ocorre por meio de uma API HTTP disponibilizada na porta 4567.

# Decisões de Implementação

## Uso de Threads

Neste projeto, foi adotada uma abordagem baseada em threads para permitir a execução simultânea de várias buscas sem bloquear o processo principal. A principal razão para essa decisão é garantir a capacidade de lidar com múltiplas solicitações concorrentes, mantendo a responsividade do sistema.
## Gerenciamento de Arquivos

O gerenciamento de arquivos é uma parte crítica deste projeto, pois os resultados das buscas precisam ser armazenados e atualizados de forma confiável. Para atender a essa necessidade, foram implementados os seguintes pontos:

- **Criação e Atualização de Arquivos**: O sistema é capaz de criar um arquivo de resultados caso não exista e atualizá-lo conforme necessário. Isso garante que os resultados das buscas sejam persistentes entre as execuções da aplicação.

- **Uso de Threads para Processamento Concorrente**: Uma das principais características da implementação é o uso de threads para lidar com múltiplas solicitações em paralelo. Para isso, foi utilizada uma estratégia de thread pooling, permitindo que várias tarefas sejam executadas ao mesmo tempo de forma controlada.

- **Gerenciamento de Arquivos e `BufferedFileWriter`**: Para cumprir os requisitos de armazenamento persistente dos resultados das buscas, foi criada a classe `BufferedFileWriter`. Essa classe é responsável por escrever os resultados em um arquivo e garante centralização de responsabilidades e a integridade dos dados durante o processo.

- **Uso de Sincronização**: Para evitar problemas de concorrência na escrita e leitura do arquivo, foi adotado um mecanismo de sincronização, onde apenas uma operação de gravação é permitida por vez. Isso garante que as atualizações sejam consistentes e seguras.

- **Utilização do `LinkedBlockingDeque`**: Para otimizar o processo de escrita em arquivo e evitar operações de I/O excessivas, foi adotado o uso de um `LinkedBlockingDeque`. Este deque atua como um buffer que permite que resultados parciais sejam armazenados de forma eficiente antes de serem gravados no arquivo. A escolha do `LinkedBlockingDeque` garante uma manipulação segura e eficaz dos resultados à medida que são produzidos.

- **Logs**: Logs foram adicionados ao código para fornecer informações úteis sobre a execução do sistema, como início da Thread de gravação, adição de objetos ao buffer e atualizações de arquivo. Isso facilita o rastreamento de eventos e depuração.

Essas decisões garantem a robustez e o desempenho da aplicação, permitindo o processamento eficiente de múltiplas buscas simultâneas e a manutenção confiável dos resultados em arquivo.


## Requisitos

### Operações suportadas:

1. **POST**: Inicia uma nova busca por um termo (keyword).
    - Requisição:
      ```
      POST /crawl HTTP/1.1
      Host: localhost:4567
      Content-Type: application/json
      Body: {"keyword": "Brazil"}
      ```
    - Resposta:
      ```
      200 OK
      Content-Type: application/json
      Body: {"id": "qbx8w8db"}
      ```

2. **GET**: Consulta resultados de busca.
    - Requisição:
      ```
      GET /crawl/qbx8w8db HTTP/1.1
      Host: localhost:4567
      ```
    - Resposta:
      ```
      200 OK
      Content-Type: application/json
      {
        "id": "qbx8w8db",
        "status": "active",
        "urls": [
          "https://www.scrapethissite.com//pages/simple/"
        ]
      }
      ```

### Requisitos Gerais:

- O termo buscado deve ter entre 4 e 32 caracteres, e a busca é case insensitive em todo o conteúdo HTML, incluindo tags e comentários.
- O id da busca é um código alfanumérico de 8 caracteres gerado automaticamente.
- A URL base do website é determinada por uma variável de ambiente.
- A aplicação suporta múltiplas buscas simultâneas e mantém informações sobre buscas em andamento ou concluídas indefinidamente.
- Enquanto uma busca está em andamento, seus resultados parciais já encontrados são retornados pela operação GET.

## Como Executar

Para compilar e iniciar a aplicação, siga os seguintes passos:

1. Build da imagem Docker:
```
docker build . -t webcrawler
```
2. Iniciar a aplicação:
```
docker run -e BASE_URL=https://www.scrapethissite.com/ -p 4567:4567 --rm webcrawler
```   
- Observação: Alterar o valor da variável de ambiente BASE_URL do comando anterior para o website que se deseja fazer a análise.


