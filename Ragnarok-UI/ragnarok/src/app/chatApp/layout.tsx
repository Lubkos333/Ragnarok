"use client";
import React, { ReactNode } from "react";
import { AppSidebar } from "@/components/chat/sidebar/app-sidebar";
import "../globals.css";
import { Separator } from "@/components/ui/separator";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { useChatStore } from "@/lib/stores/chatStore";

const ChatLayout: React.FC<{ children: ReactNode }> = ({ children }) => {
  const chats = useChatStore((state) => state.chats);
  const activeChatId = useChatStore((state) => state.activeChatId);

  const currentChat = chats.find((chat) => chat.id === activeChatId);

  return (
    <html>
      <body className="">
        <SidebarProvider>
          <AppSidebar />
          <SidebarInset>
            <header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12">
              <div className="flex items-center gap-2 px-4">
                <SidebarTrigger className="-ml-1" />
                <Separator orientation="vertical" className="mr-2 h-4" />
                <span>
                  {currentChat ? currentChat.title : "No active chat"}
                </span>
              </div>
            </header>
            {children}
          </SidebarInset>
        </SidebarProvider>
      </body>
    </html>
  );
};

export default ChatLayout;
