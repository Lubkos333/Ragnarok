"use client";

import { MessageSquareMore, CirclePlus } from "lucide-react";
import { ChatSection } from "./chat-section";
import LogoIcon from "../../logo-icon";

import { useEffect } from "react";
import { useChatStore } from "@/lib/stores/chatStore";

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "@/components/ui/sidebar";
import { NavUser } from "./nav-user";
import Link from "next/link";

// Chat section items.
const initialItems = {
  chatSection: [
    {
      title: "Nový chat",
      url: "/",
      icon: CirclePlus,
    },
    {
      title: "Chat",
      url: "/chat",
      icon: MessageSquareMore,
    },
  ],
  user: {
    name: "Thor",
    email: "thor@ragnarok.com",
    avatar: "../avatars/thorAvatar.webp",
  },
};

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { chats } = useChatStore();

  useEffect(() => {}, [chats]);

  return (
    <Sidebar collapsible="icon" {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" asChild>
              <Link href="/">
                <div className="flex aspect-square size-8 items-center justify-center rounded-lg ">
                  <LogoIcon className="text-sidebar-primary" />
                </div>
                <div className="flex flex-col gap-0.5 leading-none">
                  <span className="font-semibold">Ragnarok</span>
                  <span className="">Beta</span>
                </div>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <ChatSection chats={chats} />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={initialItems.user} />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  );
}
