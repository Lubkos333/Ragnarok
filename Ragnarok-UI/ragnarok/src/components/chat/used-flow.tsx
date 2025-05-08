import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Waypoints } from "lucide-react";

export interface UsedFlowProps {
  text: string
}

export function UsedFlow(props: UsedFlowProps) {
  const { text } = props;
  const [show, setShow] = useState(false);

  return (
    <>
    {show 
    ? <div onClick={() => setShow(!show)} className="inline-block p-2 rounded-lg bg-background text-foreground cursor-pointer"><div className="flex"><div className="pt-1 pl-1 pr-2"><Waypoints className="h-4 w-4" /></div>{text}</div></div>
    : <Button
        className=" hover:bg-background"
        variant="ghost"
        size="icon"
        title="Použitá flow"
        onClick={() => setShow(!show)}
    >
        <Waypoints className="h-4 w-4" />
    </Button>
    }
    </>
  );
}
