import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Send } from "lucide-react";
import { AnswerDto, chatApi, MessageDto } from "@/services/api/chatApi";
import { chatStore, useChatStore } from "@/lib/stores/chatStore";
import { ChatWebSocket } from "@/services/websocket";
import { CiteMessage } from "./cite-message";
import { UsedFlow } from "./used-flow";
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm'
import { toast } from "sonner";
import { LoadingBubble } from "./loadingBubble";
import { Typewriter } from "./typewriter";


export interface ChatWindowProps {
  ws: ChatWebSocket;
  isTyping: boolean;
  setIsTyping: (isTyping: boolean) => void;
  progressReport: string;
  setProgressReport: (progressReport: string) => void;
}

export function ChatWindow(props: ChatWindowProps) {
  const { ws, isTyping, setIsTyping, progressReport, setProgressReport} = props;
  const [input, setInput] = useState("");
  const sendMessage = useChatStore((state) => state.sendMessage);
  const chats = useChatStore((state) => state.chats);
  const flow = useChatStore((state) => state.flow);
  const activeChatId = useChatStore((state) => state.activeChatId);
  const numberOfParagraphs = useChatStore((state) => state.numberOfParagraphs);
  const isConnected = useChatStore((state) => state.isConnected);
  const currentChat = chats.find((chat) => chat.id === activeChatId);


  const handleSubmit = async (e: React.FormEvent) => {

    if(!isTyping) {
      e.preventDefault();
      if (input.trim()) {
        if (!isConnected) {
          toast(<div className="flex items-center gap-2 text-red-600 font-semibold">
            <span>❌ Spojení s AI selhalo.</span>
          </div>, {
            closeButton: true,
            duration: 3500,
          })
        }else {
        sendMessage(input);
        setIsTyping(true);
        const messageDto: MessageDto = {
            conversationId: activeChatId as string,
            question: input,
            flowType: flow,
            numberOfParagraphs: numberOfParagraphs
          }
          setIsTyping(true);
          setProgressReport("Zpracovávám dotaz...");
          await chatApi(ws, messageDto);
        }
      }
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] w-5/6 max-w-4xl">
      <ScrollArea className="flex-1 p-4">
        {currentChat?.messages.map((message) => (
          <div
            key={message.timestamp}
            className={`mb-4 ${
              message.sender === "user" ? "text-right" : "text-left"
            }`}
          >
            <div
              className={`inline-block p-2 rounded-lg ${
                message.sender === "user"
                  ? "bg-primary text-primary-foreground"
                  : "bg-background text-foreground"
              }`}
            >
            {
              message.sender === "user"
              ? <div className="whitespace-pre-wrap">{message.text}</div>
              : <div className="prose prose-blue max-w-none"
              style={{
                '--tw-prose-bullets': '#00000',
              } as React.CSSProperties}
              >
                      <style>
                    {`
                      .prose ul > li::marker {
                        font-size: 1.4em;
                      }
                    `}
                  </style>
                  {message.completed ? <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {(JSON.parse(message.text) as AnswerDto).answer}
                    </ReactMarkdown>:
                    <Typewriter 
                      text={(JSON.parse(message.text) as AnswerDto).answer} 
                      speed={1}
                      animateOnce={true}
                      onFinished={() => {
                        chatStore.getState().completeMessage(true);
                      }} 
                    />}
              </div>
            }
            </div>
            {message.sender === "ragnarok" && (
              <div className="mt-1 flex justify-start space-x-2">
                <UsedFlow text= {(JSON.parse(message.text) as AnswerDto).flow}/>
                <CiteMessage text= {(JSON.parse(message.text) as AnswerDto).paragraphs} />
              </div>
            )}
          </div>
        ))}
        {isTyping && (
          <div className="mb-4">
            <LoadingBubble text={progressReport} />
          </div>
        )}
      </ScrollArea>
      <form onSubmit={handleSubmit} className="p-4 border-t">
        <div className="flex items-center">
          <Input
            disabled={isTyping}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type your message..."
            className="flex-1 mr-2"
          />
          <Button type="submit" size="icon" disabled={isTyping}>
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </form>
    </div>
  );
}
