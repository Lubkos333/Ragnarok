"use client";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { useChatStore } from "@/lib/stores/chatStore";
import { ChatWindow } from "@/components/chat/chat-window";
import { OnboardingModal } from "@/components/chat/onboarding-modal";
import { useOnboardingStore } from "@/lib/stores/onBoardingStore";
import { chatApi } from "@/services/api/chatApi";
import { ChatWebSocket } from "@/services/websocket";

const commonQuestions = [
  "Jaká jsou základní práva zaměstnanců v České republice?",
  "Jak založit firmu v České republice?",
  "Jaké jsou daňové povinnosti pro malé podniky v České republice?",
];

const ChatApp = () => {
  const {
    // chats,
    activeChatId,
    createChat,
    // deleteChat,
    // setActiveChat,
    sendMessage,
  } = useChatStore();

  const showOnboarding = useOnboardingStore((state) => state.showOnboarding);
  const setOnboarding = useOnboardingStore((state) => state.setOnboarding);
  const ws = ChatWebSocket.getInstance();

  return (
    <div className="flex-1 flex flex-col w-full bg-secondary">
      <main className="flex-1 p-6 justify-center items-center flex">
        {!activeChatId ? (
          <div className="max-w-2xl mx-auto space-y-6">
            <h2 className="text-5xl font-bold text-center">Vítejte</h2>
            <p className="text-center text-primary">
              S čím vám dnes mohu pomoci?
            </p>
            <Input
              placeholder="Zde napište svůj dotaz..."
              className="max-w-xl mx-auto"
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  const message = (e.target as HTMLInputElement).value;
                  const title = message.slice(0, 20);
                  createChat(title);
                  sendMessage(message);
                  chatApi(ws ,message).then((response) => {
                    sendMessage(response.response, true);
                  });
                }
              }}
            />
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {commonQuestions.map((question, index) => (
                <Card
                  key={index}
                  className="cursor-pointer hover:bg-accent"
                  onClick={() => {
                    const message = question;
                    const title = message.slice(0, 20);
                    createChat(title);
                    sendMessage(message);
                    chatApi(ws, message).then((response) => {
                      sendMessage(response.response, true);
                    });
                  }}
                >
                  <CardHeader>
                    <CardTitle className="text-sm">{question}</CardTitle>
                  </CardHeader>
                </Card>
              ))}
            </div>
          </div>
        ) : (
          <ChatWindow ws={ws}/>
        )}
      </main>
      <OnboardingModal
        isOpen={showOnboarding}
        onClose={() => setOnboarding(false)}
      />
    </div>
  );
};

export default ChatApp;
