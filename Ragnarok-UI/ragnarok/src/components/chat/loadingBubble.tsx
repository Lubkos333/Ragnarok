import React from "react";
import { Typewriter } from "./typewriter";

export const LoadingBubble = ({ text }: { text?: string }) => {
  return (
    <div className="flex items-start gap-3">
      <div className="w-8 h-8 rounded-full bg-primary/20" />
      <div className="px-4 py-3 rounded-xl bg-muted text-foreground shadow-sm max-w-md">
        <div className="flex space-x-2 items-center py-1">
        <div className="flex space-x-2 items-center py-1">
          <span className="w-2 h-2 bg-zinc-500 rounded-full animate-bounce"></span>
          <span className="w-2 h-2 bg-zinc-500 rounded-full animate-bounce [animation-delay:150ms]"></span>
          <span className="w-2 h-2 bg-zinc-500 rounded-full animate-bounce [animation-delay:300ms]"></span>
        </div>
        {text && (
          <div className="leading-5 min-h-5">
              <Typewriter text={text} speed={20} animateOnce={false} />
          </div>
        )}
        </div>
      </div>
    </div>
  );
};
