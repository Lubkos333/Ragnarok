import React from "react";
import { ChevronDown } from "lucide-react";

const steps = [
  {
    title: "Položte Otázku",
    description: "Napište svůj právní dotaz běžným jazykem.",
  },
  {
    title: "AI Analýza",
    description:
      "Náš RAG systém zpracuje vaši otázku a prohledá českou právní databázi.",
  },
  {
    title: "Získejte Odpověď",
    description:
      "Obdržíte jasnou a stručnou odpověď s odkazy na příslušné zákony.",
  },
  {
    title: "Prozkoumejte Dále",
    description:
      "Ponořte se hlouběji do souvisejících právních témat nebo položte doplňující otázky.",
  },
];

interface HowItWorksProps {
  id?: string;
}

export default function HowItWorks({ id }: HowItWorksProps) {
  return (
    <section
      id={id}
      className="py-20 px-4 sm:px-6 lg:px-8 bg-accent-primary-mid text-background"
    >
      <h2 className="text-3xl font-bold text-center text-background">
        Jak To Funguje
      </h2>
      <div className="max-w-4xl mx-auto mt-16">
        {steps.map((step, index) => (
          <div key={index} className="flex items-start mb-8 gap-4">
            <div className="flex flex-col items-center">
              <div className="bg-background text-foreground rounded-full w-8 h-8 flex items-center justify-center flex-shrink-0 mx-auto">
                {index + 1}
              </div>
              {index < steps.length - 1 && (
                <ChevronDown className="h-6 w-6 text-secondary mx-auto mt-1" />
              )}
            </div>
            <div>
              <h3 className="text-xl font-semibold mb-2">{step.title}</h3>
              <p className="text-muted">{step.description}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
