"use client";

import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
} from "@/components/ui/sidebar";
import { MessageSquareMore, PlusCircle, Trash2 } from "lucide-react";
import { Chat } from "@/types/chat.interface";

import { useChatStore } from "@/lib/stores/chatStore";
import { Separator } from "@radix-ui/react-separator";
import { Button } from "@/components/ui/button";

export function ChatSection({ chats }: { chats: Chat[] }) {
  const setActiveChat = useChatStore((state) => state.setActiveChat);
  const deleteChat = useChatStore((state) => state.deleteChat);

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
        <Separator className="h-[1px] my-2 bg-muted" />
        {chats.map((chat) => (
          <SidebarMenuItem key={chat.id}>
            <SidebarMenuButton
              onClick={() => {
                setActiveChat(chat.id);
              }}
            >
              <MessageSquareMore />
              {chat.title}
              <Button
                onClick={() => {
                  deleteChat(chat.id);
                }}
                className="p-1 bg-transparent ml-auto text-secondary hover:text-destructive"
              >
                <Trash2 />
              </Button>
            </SidebarMenuButton>
            <SidebarMenuSub />
          </SidebarMenuItem>
        ))}
      </SidebarMenu>
    </SidebarGroup>
  );
}
