"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Send, ThumbsUp, ThumbsDown, ListRestart } from "lucide-react";
import { chatApi } from "@/services/api/chatApi";
import { useChatStore } from "@/lib/stores/chatStore";
import { ChatWebSocket } from "@/services/websocket";

export interface ChatWindowProps {
  ws: ChatWebSocket;
}

export function ChatWindow(props: ChatWindowProps) {
  const { ws } = props;
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const sendMessage = useChatStore((state) => state.sendMessage);
  const chats = useChatStore((state) => state.chats);
  const activeChatId = useChatStore((state) => state.activeChatId);

  const currentChat = chats.find((chat) => chat.id === activeChatId);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim()) {
      sendMessage(input);
      setIsTyping(true);
      chatApi(ws, input).then((response) => {
        setInput("");
        setIsTyping(false);
        sendMessage(response.response, true);
      })
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
              {message.text}
            </div>
            {message.sender === "ragnarok" && (
              <div className="mt-1 flex justify-start space-x-2">
                <Button
                  className=" hover:bg-background"
                  variant="ghost"
                  size="icon"
                  title="Kladné hodnocení"
                >
                  <ThumbsUp className="h-4 w-4" />
                </Button>
                <Button
                  className=" hover:bg-background"
                  variant="ghost"
                  size="icon"
                  title="Záporné hodnocení"
                >
                  <ThumbsDown className="h-4 w-4" />
                </Button>
                <Button
                  className=" hover:bg-background"
                  variant="ghost"
                  size="icon"
                  title="Znovu vygenerovat odpověď"
                >
                  <ListRestart className="h-4 w-4" />
                </Button>
              </div>
            )}
          </div>
        ))}
        {isTyping && (
          <div className="text-left mb-4">
            <div className="inline-block p-2 rounded-lg bg-muted">
              AI is typing...
            </div>
          </div>
        )}
      </ScrollArea>
      <form onSubmit={handleSubmit} className="p-4 border-t">
        <div className="flex items-center">
          <Input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type your message..."
            className="flex-1 mr-2"
          />
          <Button type="submit" size="icon">
            <Send className="h-4 w-4" />
          </Button>
        </div>
      </form>
    </div>
  );
}
