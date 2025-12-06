import { useState, useEffect, useRef } from "react";
import remarkGfm from 'remark-gfm'
import ReactMarkdown from 'react-markdown';

export function useTypewriter(
  text: string,
  speed: number,
  onDone?: () => void,
  animateOnce: boolean = true
) {
  const [displayed, setDisplayed] = useState("");
  const animatedOnce = useRef(false);

  useEffect(() => {
    // Pokud se má animovat jen jednou, a animace už proběhla → zobraz naráz
    if (animateOnce && animatedOnce.current) {
      setDisplayed(text);
      return;
    }

    if (!text) {
      setDisplayed("");
      return;
    }

    let index = 0;

    const interval = setInterval(() => {
      setDisplayed(text.slice(0, index));
      index++;

      if (index > text.length) {
        clearInterval(interval);

        if (animateOnce) {
          animatedOnce.current = true;
        }

        if (onDone) onDone();
      }
    }, speed);

    return () => clearInterval(interval);
  }, [text, speed, animateOnce, onDone]);

  return displayed;
}


export const Typewriter = ({
  text,
  speed,
  onFinished,
  animateOnce = true,
}: {
  text: string;
  speed: number;
  onFinished?: () => void;
  animateOnce?: boolean;
}) => {
  const output = useTypewriter(text, speed, onFinished, animateOnce);

  return (
    <span>
      <ReactMarkdown remarkPlugins={[remarkGfm]}>
        {output}
      </ReactMarkdown>
    </span>
  );
};

