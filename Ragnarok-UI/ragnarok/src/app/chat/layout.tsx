import React, { ReactNode } from "react";
// import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
// import { AppSidebar } from "@/components/app-sidebar";
import "../globals.css";

const ChatLayout: React.FC<{ children: ReactNode }> = ({ children }) => {
  return <> {children} </>;
  // <SidebarProvider>
  //   <AppSidebar />
  //   <main>
  //     <SidebarTrigger />
  //     {children}
  //   </main>
  // </SidebarProvider>
};

export default ChatLayout;
