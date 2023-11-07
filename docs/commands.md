#### **DevAin StandAlone**

해당 문서에서는 DevAin Standalone 기준 명령어를 확인할 수 있습니다.

해당 문서는 DevAin Standalone 1.2.0 (Grenade Muffin)을 기준으로 작성되었습니다.

##### Devain

| 명령어         | 설명                        |
|-------------|---------------------------|
| /status     | DevAin 봇의 상태를 확인합니다.      |
| /update-log | DevAin 봇의 업데이트 로그를 확인합니다. |

##### GPT Chat

| 명령어                         | 설명                                                                                       |
|-----------------------------|------------------------------------------------------------------------------------------|
| /ask [Content]              | ChatGPT에게 질문합니다. 해당 명령어의 기본 값은 gpt-3.5-turbo입니다. 해당 명령어는 단일 세션이며, 이전 대화를 기록하지 않습니다.      |
| /ask-fast [Content]         | ChatGPT에게 질문합니다. 해당 명령어의 기본 값은 gpt-4-0613입니다. 해당 명령어는 단일 세션이며, 이전 대화를 기록하지 않습니다.         |
| /ask-more-fast [Content]    | ChatGPT에게 질문합니다. 해당 명령어의 기본 값은 gpt-4-1106-preview입니다. 해당 명령어는 단일 세션이며, 이전 대화를 기록하지 않습니다. |
| /ask-more [Content]         | ChatGPT에게 질문합니다. 해당 명령어의 기본 값은 gpt-4입니다. 해당 명령어는 단일 세션이며, 이전 대화를 기록하지 않습니다.              |
| /askto [Model] [Contents]   | ChatGPT의 특정 모델에게 질문합니다.                                                                  |
| /edit [Instruction] [Input] | OpenAI의 InstructionGPT를 이용하여 데이터를 생성합니다.                                                 | 

##### GPT Chat Preset

| 명령어                                   | 설명                 |
|---------------------------------------|--------------------|
| /preset user                          | 자신의 프리셋 목록을 확인합니다. |
| /preset user create [Name] [Preset]   | 새 유저 프리셋을 생성합니다.   |
| /preset user delete [Name]            | 유저 프리셋을 삭제합니다.     |
| /preset server                        | 서버 프리셋 목록을 확인합니다.  |
| /preset server create [Name] [Preset] | 새 서버 프리셋을 생성합니다.   |
| /preset server delete [Name]          | 서버 프리셋을 삭제합니다.     |

##### Dall-E Generation

| 명령어                         | 설명                                       |
|-----------------------------|------------------------------------------|
| /imagine [Prompt]           | 프롬프트를 기준으로 Dall-E를 이용하여 이미지를 생성합니다.      | 
| /edit [Instruction] [Input] | OpenAI의 InstructionGPT를 이용하여 데이터를 생성합니다. | 
| /dream [engine] [Prompt]    | DreamStudio API를 이용해 이미지를 생성합니다.         |

##### DreamBooth Image Generation

| 명령어                         | 설명                                       |
|-----------------------------|------------------------------------------|
| /dream [engine] [Prompt]    | DreamStudio API를 이용해 이미지를 생성합니다.         |