"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Pilcrow } from "lucide-react";
<<<<<<< HEAD

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
=======
import ReactMarkdown from 'react-markdown';

export interface CiteMessageProps {
  text: string
}

export function CiteMessage(props: CiteMessageProps) {
  const { text } = props;
  const [show, setShow] = useState(false);

  return (
    <>
  {show 
    ? <div onClick={() => setShow(!show)} className="inline-block p-2 rounded-lg bg-background text-foreground cursor-pointer"><div className="flex"><div className="pt-1 pl-1 pr-2"><Pilcrow className="h-4 w-4" /></div>
    {/* <div className="whitespace-pre-wrap">{text}</div> */}
    <div className="prose max-w-none"
    style={{
      '--tw-prose-bullets': '#00000',
    } as React.CSSProperties}
    >
      <ReactMarkdown>
      {text}
      </ReactMarkdown>
    </div>
    </div></div>
>>>>>>> origin/main
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
