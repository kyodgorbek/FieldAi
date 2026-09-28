import SwiftUI
import Shared

struct ContentView: View {
    var body: some View {
        AssistantView()
    }
}

struct AssistantView: View {
    @StateObject private var observable = AssistantObservable()
    @State private var inputText: String = ""
    @State private var isAnimatingTyping = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("AI")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 8)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color(red: 0.47, green: 0.66, blue: 1.0), Color(red: 0.35, green: 0.45, blue: 0.95)]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .cornerRadius(8)
                
                VStack(alignment: .leading, spacing: 2) {
                    Text("FieldAI Assistant")
                        .font(.headline)
                    
                    HStack(spacing: 4) {
                        Circle()
                            .fill(Color.green)
                            .frame(width: 8, height: 8)
                        Text("Online")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                Spacer()
            }
            .padding()
            .background(Color(UIColor.systemBackground).opacity(0.95))
            .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
            
            // Messages
            ScrollViewReader { proxy in
                ScrollView {
                    VStack(spacing: 16) {
                        // Demo message to show design
                        MessageBubbleSwiftUI(
                            content: "Hello! I am FieldAI. How can I assist you in the field today?",
                            isUser: false
                        )
                        
                        if !observable.messages.isEmpty {
                            ForEach(observable.messages, id: \.id) { message in
                                MessageBubbleSwiftUI(
                                    content: message.content,
                                    isUser: message.role == .user
                                )
                                .id(message.id)
                            }
                        }
                    }
                    .padding()
                }
                .onChange(of: observable.messages.count) { _ in
                    if let last = observable.messages.last {
                        withAnimation {
                            proxy.scrollTo(last.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            // Input
            VStack {
                HStack(spacing: 12) {
                    Button(action: { 
                        // Toggle voice 
                        observable.viewModel.onAction(action: AssistantUiActionToggleVoiceListening.shared)
                    }) {
                        Image(systemName: "mic.fill")
                            .font(.system(size: 20))
                            .foregroundColor(Color(UIColor.systemGray2))
                            .padding(8)
                    }
                    
                    TextField("Ask FieldAI...", text: $inputText)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(20)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.blue.opacity(inputText.isEmpty ? 0 : 0.5), lineWidth: 1)
                        )
                    
                    Button(action: {
                        if !inputText.isEmpty {
                            observable.viewModel.onAction(action: AssistantUiActionSendMessage(text: inputText))
                            inputText = ""
                        }
                    }) {
                        Image(systemName: "arrow.up")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .padding(10)
                            .background(inputText.isEmpty ? Color.gray.opacity(0.5) : Color.blue)
                            .clipShape(Circle())
                            .shadow(color: inputText.isEmpty ? Color.clear : Color.blue.opacity(0.3), radius: 4, x: 0, y: 2)
                    }
                    .disabled(inputText.isEmpty)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .background(Color(UIColor.systemBackground))
            .shadow(color: Color.black.opacity(0.05), radius: -5, x: 0, y: -2)
        }
        .background(
            LinearGradient(
                gradient: Gradient(colors: [Color(UIColor.systemGroupedBackground), Color(UIColor.secondarySystemGroupedBackground)]),
                startPoint: .top,
                endPoint: .bottom
            )
        )
    }
}

struct MessageBubbleSwiftUI: View {
    let content: String
    let isUser: Bool
    
    var body: some View {
        HStack {
            if isUser {
                Spacer(minLength: 40)
                Text(content)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [Color(red: 0.1, green: 0.4, blue: 1.0), Color(red: 0.2, green: 0.5, blue: 1.0)]),
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(color: Color.blue.opacity(0.2), radius: 4, x: 0, y: 2)
            } else {
                Text(content)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color(UIColor.systemBackground))
                    .foregroundColor(Color(UIColor.label))
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
                Spacer(minLength: 40)
            }
        }
    }
}

class AssistantObservable: ObservableObject {
    @Published var messages: [Message] = []
    let viewModel: AssistantViewModel

    init() {
        // Initialize ViewModel via Koin DI
        self.viewModel = KoinHelper().assistantViewModel
        
        // Note: Observing Kotlin StateFlow natively in Swift requires wrapper extensions.
        // For Phase 6 design demonstration, we initialize the View UI components.
    }
}