# Helidon Assistant

Helidon Assistant is a Retrieval-Augmented Generation (RAG) application built around Helidon's documentation. It provides a friendly AI chat interface to help answer Helidon-related questions.

Key Features:

- **Smart AsciiDoc Processing**: Special handling of AsciiDoc content during embedding creation, including:
    - Clean conversion to plain text.
    - Grouping by document sections.
    - Preserving code snippets and tables as meaningful units.
- **Metadata Preservation**: Embeddings retain rich metadata such as:
    - Document name.
    - Section and position within the document.
- **Stateless Backend**: Conversations are summarized on the server and stored client-side for a lightweight, scalable experience.

## Getting Started

### 1. Clone the Helidon Documentation

First, clone the [Helidon GitHub repository](https://github.com/helidon-io/helidon) to a temporary location on your local machine.

### 2. Configure the Application

Update the project's `application.yaml` file to point to the Helidon documentation source, typically located at `{helidon_repo_root}/docs/src/main/asciidoc`. You can also define inclusion and exclusion filters as needed:

```yaml
app:
  root: "//{helidon_repo_root}/docs/src/main/asciidoc"
#  exclusions:
  inclusions: "*.adoc"
```

### 3. Build the Project

Use Maven to build the application:

```bash
mvn clean package
```

### 4. Run the Application

Launch the assistant with:

```bash
# Run Oracle DB
docker run --name 23ai -p 1521:1521 -e ORACLE_PASSWORD=free -e APP_USER=vector -e APP_USER_PASSWORD=vector gvenzl/oracle-free:23-slim

java -jar target/helidon-assistant.jar
```

### 5. Chat with the Assistant

Once the application is running, open your browser and navigate to [http://localhost:8080](http://localhost:8080) to start chatting with the Helidon Assistant.

Use the chat interface to ask questions and get helpful insights based on Helidon's official documentation.
