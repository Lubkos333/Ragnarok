import React from "react";
import {
  Accordion,
  AccordionItem,
  AccordionTrigger,
  AccordionContent,
} from "@/components/ui/accordion";

interface FaqProps {
  id?: string;
}

export default function Faq({ id }: FaqProps) {
  return (
    <section id={id} className="py-32 bg-background">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-3xl mx-auto mb-20">
          <h2 className="text-4xl font-bold text-accent-primary-dark mb-6">
            Často Kladené Otázky
          </h2>
          <p className="text-primary">Vše, co potřebujete vědět o RagNaRok</p>
        </div>
        <Accordion type="single" collapsible className="max-w-2xl mx-auto">
          {[
            {
              question: "Jak přesné jsou informace?",
              answer:
                "RagNaRok využívá pokročilou AI technologii a oficiální databázi eSbírky pro poskytování přesných informací. Nemělo by to však být považováno za právní poradenství.",
            },
            {
              question: "Jaké typy otázek mohu pokládat?",
              answer:
                "Můžete se ptát na české zákony, předpisy, právní postupy a obecné právní koncepty. Systém je navržen tak, aby zvládl širokou škálu právních témat.",
            },
            {
              question: "Jak je chráněno moje soukromí?",
              answer:
                "Používáme nejmodernější šifrování a opatření na ochranu dat. Vaše konverzace jsou důvěrné a nikdy nejsou sdíleny s třetími stranami.",
            },
          ].map((faq, index) => (
            <AccordionItem
              key={index}
              value={`item-${index + 1}`}
              className="border-b border-gray-200"
            >
              <AccordionTrigger className="text-lg font-medium text-accent-primary-dark hover:text-accent-secondary transition-colors">
                {faq.question}
              </AccordionTrigger>
              <AccordionContent className="text-primary">
                {faq.answer}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </div>
    </section>
  );
}
