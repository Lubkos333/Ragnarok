import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { BrainCircuit, LucideProps, MessagesSquare, Users } from "lucide-react";

interface Features {
  title: string;
  description: string;
  icon: React.ForwardRefExoticComponent<
    Omit<LucideProps, "ref"> & React.RefAttributes<SVGSVGElement>
  >;
}

interface FeaturesProps {
  id?: string;
}

const features: Features[] = [
  {
    title: "Integrace Umělé Inteligence",
    description:
      "Využívá pokročilé AI modely pro automatizované analýzy v reálném čase.",
    icon: BrainCircuit,
  },
  {
    title: "Nástroj Pro Každého",
    description:
      "Navrženo pro právní profesionály i širokou veřejnost, zpřístupňuje právo všem.",
    icon: Users,
  },
  {
    title: "Přirozená Konverzace",
    description:
      "Umožňuje vám komunikovat s platformou běžnou řečí a získávat srozumitelné odpovědi, což zjednodušuje práci i v komplexních oblastech.",
    icon: MessagesSquare,
  },
];

export default function Features({ id }: FeaturesProps) {
  return (
    <section
      id={id}
      className="w-full min-h-[70vh] flex flex-col items-center bg-muted justify-center py-16"
    >
      <div className="flex flex-col items-center justify-center max-w-7xl gap-16">
        <div className="flex flex-col text-center items-center justify-center gap-4">
          <h2 className="text-3xl font-bold">Revoluce v Právním Poradenství</h2>
          <p className="text-lg">
            Zažijte budoucnost právní asistence s našimi špičkovými funkcemi
          </p>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 max-w-7xl gap-4 place-content-stretch">
          {features.map((feature, index) => (
            <Card
              key={index}
              className="p-6 min-h-72 lg:min-h-96 max-w-lg lg:max-w-xs shadow-lg hover:shadow-2xl hover:shadow-secondary"
            >
              <CardHeader>
                <feature.icon className="text-accent-primary size-16" />
                <CardTitle>
                  <h3 className="text-xl font-bold">{feature.title}</h3>
                </CardTitle>
              </CardHeader>

              <CardContent>
                <p>{feature.description}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
