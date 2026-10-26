# ID-ENTUTA / SAFEIDENDURANCE — projetos móveis

Estado: **base nativa para desenvolvimento e homologação, ainda não compilada,
assinada ou pronta para submissão**. Não contém IPA, APK ou AAB.

Os projetos preservam o aplicativo web do Sprint 2 dentro de WKWebView (iOS)
e WebView (Android), com navegação nativa, atualização, mensagem de falha de conexão
e compartilhamento nativo da entrada do app. Não compartilham a URL de telas
privadas. Não existe ponte de JavaScript para executar comandos nativos.
O app depende de internet e do backend disponível.

## Configuração antes de compilar

- O identificador provisório é `com.safeidendurance.atleta`.
- O endereço definitivo confirmado é `https://www.safeidendurance.com/atleta`.
- Confirme titularidade e disponibilidade do identificador antes de registrá-lo.
- Configure URL no `android/app/build.gradle` e `ios/project.yml`.
- O Sprint 2 precisa ser publicado nesse endereço com banco e autenticação de
  produção homologados. O domínio existente, sozinho, não publica a rota nova.
- Não use endereço trycloudflare, banco fictício ou credenciais de demonstração
  nos pacotes de distribuição.

## Android

Abra a pasta `android/` no Android Studio. Instale o SDK Android 36,
use JDK 17 e Gradle 8.13 para o plugin Android 8.13.0. O wrapper Gradle
ainda precisa ser gerado pelo ambiente de build (`gradle wrapper --gradle-version 8.13`).

Para testar: `gradle :app:assembleDebug`.
Para o Google Play: configure uma chave de upload no ambiente de build e gere
o Android App Bundle assinado por Android Studio → Generate Signed Bundle / APK.
Não guarde senhas, chaves ou certificados no repositório.

## Compilação na nuvem, sem Mac próprio

O titular confirmou que tem contas Apple Developer e Google Play Console,
mas não possui Mac. O workflow `.github/workflows/build-mobile.yml` usa
um runner macOS do GitHub para validar o iOS e um runner Linux para Android.
Copie **o conteúdo desta pasta mobile**, incluindo `.github`, para a raiz do
repositório escolhido. Execute Actions → Compilar apps de homologação → Run workflow.
Não há upload automático para lojas.

Os artefatos são APK de depuração (instalável para testes Android), AAB ainda sem
assinatura de distribuição e app de simulador iOS. O artefato do simulador NÃO
instala em iPhone e NÃO é um IPA de TestFlight. A compilação remota ainda não
foi executada. O repositório de destino precisa ser informado.

Para produzir IPA/TestFlight depois da primeira compilação, será necessário
configurar equipe Apple, certificado de distribuição e perfil, ou assinatura
automática autorizada. No Android, configurar a chave de upload. Os segredos
devem entrar pelo cofre da plataforma de CI, nunca em mensagens ou arquivos do projeto.
O uso de runners pode consumir a franquia ou gerar custos da conta GitHub.

## iOS

Em um Mac com Xcode e XcodeGen instalados:

```sh
cd ios
xcodegen generate
open IDEntuta.xcodeproj
```

Selecione a equipe Apple Developer, confirme o Bundle ID e teste no iPhone.
Depois use Product → Archive → Distribute App → App Store Connect para TestFlight.
Para validar compilação sem assinatura:

```sh
xcodebuild -project IDEntuta.xcodeproj -scheme IDEntuta -sdk iphonesimulator CODE_SIGNING_ALLOWED=NO build
```

## Pendências obrigatórias antes de enviar às lojas

1. Compilar ambos os projetos e testar em aparelhos reais; estes fontes não
   foram compilados no ambiente Windows atual, que não possui SDK Android/Xcode.
2. Confirmar endereço, identificadores, contas das lojas, certificados e assinatura.
3. Homologar login, logout, retorno do segundo plano, QR e download/compartilhamento,
   troca de senha, cadastro, WhatsApp, recuperação de conexão e navegação externa.
   Downloads do QR no WebView ainda precisam de implementação/validação nativa.
4. A localização do Emergency Assistant não está habilitada na camada nativa;
   antes de distribuir, decidir e implementar permissões e consentimento ou abrir
   esse atendimento no navegador. Nenhum resultado de socorro deve depender disso.
5. Implementar início da exclusão da própria conta no app e o fluxo web exigido
   pelo Google Play; o código atual não comprova esse requisito.
6. Publicar política de privacidade e termos com responsável e contato reais.
   Preencher App Privacy e Data Safety segundo o tratamento efetivo de dados
   pessoais, saúde, contatos e localização, incluindo serviços do backend.
7. Avaliar funcionalidade mínima: envolver o portal em WebView e adicionar uma
   barra não garante aprovação Apple. Definir e homologar recursos móveis úteis.
8. Manter compra de serviços digitais indisponível até definir as regras de
   cobrança aplicáveis em cada loja. O catálogo do Sprint 2 é somente consulta;
   links legados de compra/cadastro precisam ser revisados na versão distribuída.
9. Criar descrição, classificação etária, screenshots reais, URL de suporte e
   conta de revisão com dados fictícios, no backend definitivo de homologação.
10. Distribuir primeiro por TestFlight e teste interno/fechado do Google Play;
    publicar somente após aceite. Nenhuma submissão foi realizada.

## Referências oficiais consultadas em 26/10/2026

- https://developer.apple.com/app-store/review/guidelines/
- https://developer.apple.com/support/offering-account-deletion-in-your-app/
- https://developer.android.com/develop/ui/views/layout/webapps/webview
- https://support.google.com/googleplay/android-developer/answer/14151465

Para contas pessoais novas no Google Play, verifique a exigência de teste fechado
com pelo menos 12 participantes por 14 dias consecutivos antes de pedir acesso
à produção. A regra depende do tipo e da data da conta.
