import SwiftUI
import WebKit

@main
struct IDEntutaApp: App {
    var body: some Scene { WindowGroup { PortalScreen() } }
}

final class PortalModel: NSObject, ObservableObject, WKNavigationDelegate, WKUIDelegate {
    let web: WKWebView
    let entry: URL
    @Published var failed = false

    override init() {
        guard let text = Bundle.main.object(forInfoDictionaryKey: "PortalURL") as? String,
              let url = URL(string: text), url.scheme == "https", url.host != nil else {
            fatalError("Configure PortalURL com o domínio HTTPS homologado.")
        }
        entry = url
        let config = WKWebViewConfiguration()
        config.websiteDataStore = .default()
        web = WKWebView(frame: .zero, configuration: config)
        super.init()
        web.navigationDelegate = self
        web.uiDelegate = self
        web.allowsBackForwardNavigationGestures = true
        loadHome()
    }
    func loadHome() {
        failed = false
        web.load(URLRequest(url: entry, cachePolicy: .reloadIgnoringLocalCacheData))
    }
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        if (error as NSError).code != NSURLErrorCancelled { failed = true }
    }
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        if (error as NSError).code != NSURLErrorCancelled { failed = true }
    }
    func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction,
                 decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        guard let url = action.request.url else { decisionHandler(.cancel); return }
        if url.scheme == "https", url.host == entry.host, url.port == nil || url.port == 443 {
            decisionHandler(.allow)
        } else {
            decisionHandler(.cancel)
            if action.targetFrame?.isMainFrame != false { openExternal(url) }
        }
    }
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration,
                 for action: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        if action.targetFrame == nil, let url = action.request.url {
            if url.scheme == "https", url.host == entry.host { webView.load(action.request) }
            else { openExternal(url) }
        }
        return nil
    }
    private func openExternal(_ url: URL) {
        if ["https", "tel", "mailto"].contains(url.scheme ?? "") { UIApplication.shared.open(url) }
    }
}

struct PortalWebView: UIViewRepresentable {
    let model: PortalModel
    func makeUIView(context: Context) -> WKWebView { model.web }
    func updateUIView(_ view: WKWebView, context: Context) {}
}

struct PortalScreen: View {
    @StateObject private var model = PortalModel()
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if model.failed {
                    Text("Não foi possível conectar. Verifique a internet e toque em Atualizar.")
                        .padding().accessibilityAddTraits(.isStaticText)
                }
                PortalWebView(model: model)
            }
            .navigationTitle("ID-ENTUTA")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .bottomBar) {
                    Button { if model.web.canGoBack { model.web.goBack() } } label: { Label("Voltar", systemImage: "chevron.left") }
                    Spacer()
                    Button { model.loadHome() } label: { Label("Início", systemImage: "house") }
                    Spacer()
                    Button { model.failed = false; model.web.reload() } label: { Label("Atualizar", systemImage: "arrow.clockwise") }
                    Spacer()
                    ShareLink(item: model.entry) { Label("Compartilhar app", systemImage: "square.and.arrow.up") }
                }
            }
        }
    }
}
