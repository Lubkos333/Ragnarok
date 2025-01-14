"use client";

import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
} from "@/components/ui/sidebar";
import { MessageSquareMore, PlusCircle } from "lucide-react";
import { Chat } from "@/types/chat.interface";

import { useChatStore } from "@/lib/stores/chatStore";
import { Separator } from "@radix-ui/react-separator";

export function ChatSection({ chats }: { chats: Chat[] }) {
  const setActiveChat = useChatStore((state) => state.setActiveChat);

  return (
    <SidebarGroup>
      <SidebarGroupLabel>Chaty</SidebarGroupLabel>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton onClick={() => setActiveChat(null)}>
            <PlusCircle />
            Nový Chat
          </SidebarMenuButton>
        </SidebarMenuItem>
        <Separator />
        {chats.map((chat) => (
          <SidebarMenuItem key={chat.id}>
            <SidebarMenuButton
              onClick={() => {
                setActiveChat(chat.id);
              }}
            >
              <MessageSquareMore />
              {chat.title}
            </SidebarMenuButton>
            <SidebarMenuSub />
          </SidebarMenuItem>
        ))}
      </SidebarMenu>
    </SidebarGroup>
  );
}
