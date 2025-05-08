"use client";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { useChatStore } from "@/lib/stores/chatStore";
import { ChatWindow } from "@/components/chat/chat-window";
import { OnboardingModal } from "@/components/chat/onboarding-modal";
import { useOnboardingStore } from "@/lib/stores/onBoardingStore";
import { chatApi, MessageDto } from "@/services/api/chatApi";
import { ChatWebSocket } from "@/services/websocket";

import { toast } from "sonner";
import { Toaster } from "@/components/ui/sonner";
import { useEffect } from "react";


const commonQuestions = [
  "Jak se dědí, když někdo nemá závěť?",
  "Mám nárok na náhradu mzdy, když jsem doma kvůli nemoci?",
  "Může mi zaměstnavatel nařídit práci přes víkend?",
  "Je trestný čin, když si vezmu nalezenou peněženku a nahlásím ji později?",
  "Jak probíhá výslech svědka na policii?",
  "Může mě zaměstnavatel propustit kvůli tomu, že jsem byl obviněn z trestného činu?"
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
  const numberOfParagraphs = useChatStore((state) => state.numberOfParagraphs);
  const isTyping = useChatStore((state) => state.isTyping);
  const setIsTyping = useChatStore((state) => state.setIsTyping);
  const ws = ChatWebSocket.getInstance();

  //const ws = ChatWebSocket.getInstance();
  const isConnected = useChatStore((state) => state.isConnected);
  //const [isTyping, setIsTyping] = useState(false);

  useEffect(() => {
    if(!isConnected && isTyping) {
      toast(<div className="flex items-center gap-2 text-red-600 font-semibold">
                <span>❌ Spojení bylo dočasně přerušeno. Zkuste prosím odeslat dotaz znovu.</span>
              </div>, {
                closeButton: true,
                duration: 5000,
              });
      setIsTyping(false);
    }
  }, [isConnected, isTyping, setIsTyping])


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
                    if (!isConnected) {
                      toast(<div className="flex items-center gap-2 text-red-600 font-semibold">
                        <span>❌ Spojení s AI selhalo.</span>
                      </div>, {
                        closeButton: true,
                        duration: 3500,
                      })
                    }else {
                  const message = (e.target as HTMLInputElement).value;
                  const title = message.slice(0, 20);
                  const newChatId = await createChat(title);
                  sendMessage(message);
                  const messageDto: MessageDto = {
                    conversationId: newChatId,
                    question: message,

                    flowType: flow,
                    numberOfParagraphs: numberOfParagraphs

                  }
                  setIsTyping(true);
                  chatApi(ws ,messageDto).then((response) => {
                    sendMessage(response.response, true);
                  }).then(() => setIsTyping(false));

                }}
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

                      flowType: flow,
                      numberOfParagraphs: numberOfParagraphs,
                    }
                    setIsTyping(true);

                    chatApi(ws, messageDto).then((response) => {
                      sendMessage(response.response, true);
                    })
                    .then(() => setIsTyping(false));
                  }}}
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
        <Toaster />
      </main>
      <OnboardingModal
        isOpen={showOnboarding}
        onClose={() => setOnboarding(false)}
      />
    </div>
  );
};

export default ChatApp;
