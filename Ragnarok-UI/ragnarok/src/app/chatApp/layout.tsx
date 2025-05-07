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
import { Waypoints } from "lucide-react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuPortal, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";

const ChatLayout: React.FC<{ children: ReactNode }> = ({ children }) => {
  const chats = useChatStore((state) => state.chats);
  const activeChatId = useChatStore((state) => state.activeChatId);
  const setFlow = useChatStore((state) => state.setFlow);
  const flow = useChatStore((state) => state.flow);

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
              <div className="ml-auto pr-4">
                <DropdownMenu>
                  <DropdownMenuTrigger>
                    <Waypoints className="h-5 w-5"/>
                </DropdownMenuTrigger>
                <DropdownMenuPortal>
                  <DropdownMenuContent>
                    <DropdownMenuItem className={`cursor-pointer ${flow === "CLASSIC" ? "bg-primary text-primary-foreground": "bg-white"}`} onSelect={() => setFlow("CLASSIC")}>
                      {"Classic flow"}
                    </DropdownMenuItem>
                    <DropdownMenuItem className={`cursor-pointer ${flow === "KEYWORDS" ? "bg-primary text-primary-foreground": "bg-white"}`} onSelect={() => setFlow("KEYWORDS")}>
                      {"Keywords flow"}
                    </DropdownMenuItem>
                    <DropdownMenuItem className={`cursor-pointer ${flow === "PARAPHRASE" ? "bg-primary text-primary-foreground": "bg-white"}`} onSelect={() => setFlow("PARAPHRASE")}>
                      {"Paraphrase flow"}
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                  </DropdownMenuPortal>
                </DropdownMenu>
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
