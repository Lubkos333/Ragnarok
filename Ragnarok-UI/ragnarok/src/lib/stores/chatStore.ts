import { create } from "zustand";
import { persist } from "zustand/middleware";
import { v4 as uuidv4 } from "uuid";

import { Chat, Message } from "@/types/chat.interface";

interface ChatState {
  chats: Chat[];
  activeChatId: string | null; // Currently selected chat
  // activeChat: Chat | null;
  createChat: (title: string) => void;
  deleteChat: (id: string) => void;
  setActiveChat: (id: string | null) => void;
  sendMessage: (text: string, serverResponse?: boolean) => void;
}

export const useChatStore = create(
  persist<ChatState>(
    (set, get) => ({
      chats: [],
      activeChatId: null,
      // activeChat: null,

      // Create a new chat with a unique ID
      createChat: (title) => {
        const newChat: Chat = {
          id: uuidv4(),
          title,
          messages: [],
          lastUpdated: Date.now(),
        };
        set((state) => ({
          chats: [...state.chats, newChat],
          activeChatId: newChat.id,
        }));
      },

      // Delete a chat by its ID
      deleteChat: (id) => {
        set((state) => ({
          chats: state.chats.filter((chat) => chat.id !== id),
          activeChatId: state.activeChatId === id ? null : state.activeChatId, // Deselect if deleted
        }));
      },

      // Set the currently active chat
      setActiveChat: (id) => {
        set((/*state*/) => ({
          // activeChat: state.chats.find((chat) => chat.id === id) || null,
          activeChatId: id,
        }));
      },

      // Send a message to the active chat
      sendMessage: (text, serverResponse) => {
        const { activeChatId, chats } = get();
        if (!activeChatId) return;

        const newMessage: Message = {
          sender: !serverResponse ? "user" : "ragnarok",
          text,
          timestamp: Date.now(),
        };

        set({
          chats: chats.map((chat) =>
            chat.id === activeChatId
              ? {
                  ...chat,
                  messages: [...chat.messages, newMessage],
                  lastUpdated: Date.now(),
                }
              : chat
          ),
        });
      },
    }),
    {
      name: "multi-chat-storage", // LocalStorage key
    }
  )
);
