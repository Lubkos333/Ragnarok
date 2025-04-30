"use client";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { useChatStore } from "@/lib/stores/chatStore";
import { ChatWindow } from "@/components/chat/chat-window";
import { OnboardingModal } from "@/components/chat/onboarding-modal";
import { useOnboardingStore } from "@/lib/stores/onBoardingStore";
import { chatApi, MessageDto } from "@/services/api/chatApi";
import { ChatWebSocket } from "@/services/websocket";
import { useState } from "react";

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
    flow,
  } = useChatStore();

  const showOnboarding = useOnboardingStore((state) => state.showOnboarding);
  const setOnboarding = useOnboardingStore((state) => state.setOnboarding);
  const ws = ChatWebSocket.getInstance();
  const [isTyping, setIsTyping] = useState(false);

  return (
    <div className="flex-1 flex flex-col w-full bg-muted">
      <main className="flex-1 p-6 justify-center items-center flex">
        {!activeChatId ? (
          <div className="max-w-2xl mx-auto space-y-6">
            <h2 className="text-5xl font-bold text-center">Vítejte</h2>
            <p className="text-center text-primary">
              S čím vám dnes mohu pomoci?
            </p>
            <Input
              placeholder="Zde napište svůj dotaz..."
              className="max-w-xl mx-auto text-foreground hover:outline-none hover:ring-1 hover:ring-ring hover:ring-offset-2"
              onKeyDown={async (e) => {
                if (e.key === "Enter") {
                  const message = (e.target as HTMLInputElement).value;
                  const title = message.slice(0, 20);
                  const newChatId = await createChat(title);
                  sendMessage(message);
                  const messageDto: MessageDto = {
                    conversationId: newChatId,
                    question: message,
                    flowType: flow
                  }
                  setIsTyping(true);
                  chatApi(ws ,messageDto).then((response) => {
                    sendMessage(response.response, true);
                  }).then(() => setIsTyping(false));
                }
              }}
            />
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {commonQuestions.map((question, index) => (
                <Card
                  key={index}
                  className="cursor-pointer ring-offset-background hover:outline-none hover:ring-1 hover:ring-ring hover:ring-offset-2"
                  onClick={async () => {
                    const message = question;
                    const title = message.slice(0, 20);
                    const newChatId = await createChat(title);;
                    sendMessage(message);
                    const messageDto: MessageDto = {
                      conversationId: newChatId,
                      question: message,
                      flowType: flow
                    }
                    chatApi(ws, messageDto).then((response) => {
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
          <ChatWindow ws={ws} isTyping={isTyping} setIsTyping={setIsTyping} />
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
