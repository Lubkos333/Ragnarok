import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Briefcase, Home, Gavel, Building } from "lucide-react";

const useCases = [
  {
    icon: <Home className="h-6 w-6 text-accent-primary" />,
    title: "Osobní Záležitosti",
    description:
      "Řešení každodenních právních otázek týkajících se bydlení, rodinného práva nebo spotřebitelských práv.",
  },
  {
    icon: <Briefcase className="h-6 w-6 text-accent-primary" />,
    title: "Podnikání",
    description:
      "Pomoc malým a středním podnikům s právními aspekty podnikání, smlouvami a regulacemi.",
  },
  {
    icon: <Gavel className="h-6 w-6 text-accent-primary" />,
    title: "Právní Praxe",
    description:
      "Podpora právníků při výzkumu, přípravě případů a rychlém přístupu k relevantním právním informacím.",
  },
  {
    icon: <Building className="h-6 w-6 text-accent-primary" />,
    title: "Veřejná Správa",
    description:
      "Asistence úředníkům a zaměstnancům veřejné správy při navigaci složitými právními předpisy.",
  },
];

interface UseCasesProps {
  id?: string;
}

export default function UseCases({ id }: UseCasesProps) {
  return (
    <section id={id} className="py-20 px-4 sm:px-6 lg:px-8 bg-gray-50">
      <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">
        Případy Použití
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-5xl mx-auto">
        {useCases.map((useCase, index) => (
          <Card key={index}>
            <CardHeader>
              <CardTitle className="flex items-center">
                {useCase.icon}
                <span className="ml-2">{useCase.title}</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p>{useCase.description}</p>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
