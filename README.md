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

### 1. Clone agentic branch of Helidon Assistant.
```bash
git clone -b kec/agentic-assistant --single-branch git@github.com:danielkec/helidon-assistant.git
cd helidon-assistant
```
### 2. Build locally Helidon snapshot with a working version of Lc4j agentic integration.

```bash
bash ./buildHelidonSnapshot.sh
```

### 3. Build and run Helidon Assistant itself 
Ingestion is done before the server startup, it can take around 30 seconds.

```bash
bash ./buildAndRun.sh
```

### 4. Chat with the Assistant

Once the application is running, open your browser and navigate to [http://localhost:8080](http://localhost:8080) to start chatting with the Helidon Assistant.

Use the chat interface to ask questions and get helpful insights based on Helidon's official documentation.
