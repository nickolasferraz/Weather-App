
 # ChuvaApp — Documento de Requisitos

### Ideia

Cria uma aplicação aonde ira aparecer uma nuvem e mostra a rota dela e mostra as zonas de alta chuva pouca chuva e da para você pequisar aonde você vai que horas vai chover 

## Requisitos e Desing system

 - Atualização em Tempo Real
 - Alta Disponibilidade 
 - Consistencia dos Dados


## Descrição

Aplicativo web (PWA) que calcula a **rota mais segura entre dois pontos**
considerando zonas de alagamento previstas para os **próximos 30 a 60 minutos**.
O app cruza dados de previsão de chuva em tempo real com mapas históricos de
risco de inundação das regiões de São Paulo e Guarulhos, entregando ao usuário
a rota antes que o problema aconteça.

---

## Usuários

Pessoas que precisam se deslocar pela cidade e querem **evitar ficar presas
em alagamentos** antes de sair. Não é pra quem já está no trânsito — é pra
quem ainda está decidindo se sai e por onde.

---

## Requisitos

### Funcionais

 1 serviço
- Usuário informa **origem e destino** e recebe a rota mais segura
- Mapa exibe sobreposição visual das **zonas de intensidade de chuva**
- A rota considera **zonas de risco de alagamento** + 

2-
- **previsão de chuva
  para os próximos 30–60 min**
- Alertas quando uma rota salva está em risco de alagamento

3-
- **Cadastro com login** — salva histórico de pesquisas recentes
- Alertas quando uma rota salva está em risco de alagamento

### Não-Funcionais

- **Atualização em tempo real** — dados de radar com ciclo de 10 min
- **Alta disponibilidade** — app não pode cair durante chuvas intensas,
  justamente quando é mais necessário
- **Consistência dos dados** — todos os usuários veem o mesmo estado
  do radar no mesmo instante

---

## Contexto Adicional

- **Dados públicos disponíveis**
  - IPT disponibiliza carta de suscetibilidade a inundações de Guarulhos
    via WMS — não precisa construir esse dado do zero
  - GeoSampa SP tem histórico de ocorrências de alagamento com API
    WFS/WMS aberta, atualizada mensalmente
  - Prefeitura de Guarulhos tem mapa oficial de áreas de risco publicado

- **Janela de 30–60 min é intencional**
  Previsão de precipitação acima de 1h perde confiabilidade. O app não
  promete o que não pode entregar.

- **Diferencial frente ao RainViewer e Climatempo**
  Esses apps mostram onde está chovendo. O ChuvaApp responde uma pergunta
  diferente: *"o meu caminho vai alagar antes de eu chegar?"*

- **PWA first**
  Funciona no browser e é instalável no celular sem passar por loja.
  Backend não muda quando evoluir para mobile nativo.

- **Stack inicial sugerida**
  - Backend: Spring Boot 3 (Java 21)
  - Frontend: React + Leaflet.js
  - Roteamento: OpenRouteService (suporta exclusão de zonas de risco)
  - Infra: Docker + AWS EC2 + GitHub Actions


# ChuvaApp — Planilha de Características Arquiteturais

---

## 7 Características Explícitas

| # | Característica | Origem nos Requisitos |
|---|---|---|
| 1 | **Availability** | App não pode cair durante chuvas intensas — justamente quando é mais necessário |
| 2 | **Fault Tolerance** | Fallback quando APIs externas bloquearem por rate limit |
| 3 | **Reliability** | Dado correto vale mais que dado rápido — rota errada = usuário preso |
| 4 | **Elasticity** | Pico de acesso durante chuvas + controle de chamadas assíncronas às APIs |
| 5 | **Data Freshness** | Dados de radar com ciclo de atualização de 10 minutos |
| 6 | **Personalization** | Login com histórico de pesquisas recentes por usuário |
| 7 | **Responsiveness** | Rota calculada antes do usuário sair de casa |

---

## Top 3 — Características Primárias

| Posição | Característica | Justificativa |
|---|---|---|
| 🥇 1º | **Availability** | O app precisa estar no ar no pior momento — durante a chuva intensa |
| 🥈 2º | **Fault Tolerance** | Toda a proposta de valor depende de APIs externas; se elas falharem, o sistema não pode cair junto |
| 🥉 3º | **Reliability** | Dado desatualizado é mais perigoso que dado lento — usuário pode tomar rota que já alagou |

---

## Características Implícitas

| Característica | Justificativa |
|---|---|
| **Security** | Sistema com cadastro e histórico de pesquisas por usuário |
| **Maintainability** | Solo developer — arquitetura precisa ser simples de evoluir sem time |
| **Observability** | Necessário detectar quando o radar parou de atualizar ou API foi bloqueada antes que o usuário perceba |

---

## Outras Considerações

### 1. PACELC → Perfil PA/EL

O ChuvaApp adota o modelo **PACELC** por ser mais preciso que o CAP para sistemas de dados em tempo real:

- **P → A (Availability):** Em caso de partição de rede, o sistema responde com dado em cache mesmo que levemente desatualizado, ao invés de retornar erro.
- **E → L (Latency):** Durante operação normal, prefere responder rápido com dado do Redis a aguardar confirmação da API externa.

> Dado de 10 minutos atrás ainda é útil para evitar um alagamento. Essa é uma decisão consciente.

### 2. Dependência de Terceiros é o Maior Risco

Toda a proposta de valor depende de APIs externas:

- Previsão de chuva (OpenWeatherMap / Open-Meteo)
- Zonas de risco (IPT / GeoSampa SP)
- Roteamento (OpenRouteService)

Se todas falharem simultaneamente, o app perde o core. O **Fault Tolerance** no Top 3 existe exatamente por isso.
Mitigação: fallback em cascata e cache com TTL como fonte de verdade.

### 3. Restrição de Time

Solo developer por enquanto. Essa restrição:

- Justifica as implícitas de **Maintainability** e **Observability**
- Limita deliberadamente a complexidade arquitetural no MVP
- Favorece soluções com dados públicos prontos (IPT, GeoSampa) ao invés de construir modelos próprios


| Serviço         | Responsabilidade                         |
| --------------- | ---------------------------------------- |
| Weather Service | Previsão de chuva 30–60min + radar       |
| Routing Service | Calcular rota mais segura                |
| User Service    | Cadastro, login, histórico, rotas salvas |
| Alert Service   | Consome eventos e notifica usuário       |
| API Gateway     | Entrada única, JWT, roteamento           |
| Observability   | Métricas, traces, logs                   |