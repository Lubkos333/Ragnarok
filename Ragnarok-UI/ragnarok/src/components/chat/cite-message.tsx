"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Pilcrow } from "lucide-react";

export interface CiteMessageProps {
  text: string
}

export function CiteMessage(props: CiteMessageProps) {
  const { text } = props;
  const [show, setShow] = useState(false);

  return (
    <>
  {show 
    ? <div onClick={() => setShow(!show)} className="inline-block p-2 rounded-lg bg-background text-foreground cursor-pointer"><div className="flex"><div className="pt-1 pl-1 pr-2"><Pilcrow className="h-4 w-4" /></div>{text}</div></div>
    : <Button
        className=" hover:bg-background"
        variant="ghost"
        size="icon"
        title="Použitá flow"
        onClick={() => setShow(!show)}
    >
        <Pilcrow className="h-4 w-4" />
    </Button>
    }
    </>
  );
}
