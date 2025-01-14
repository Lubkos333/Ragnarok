import { Check } from "lucide-react";
import { Button } from "@/components/ui/button";

interface pricingProps {
  id?: string;
}

const plans = [
  {
    name: "Základní",
    price: "Zdarma",
    features: [
      "10 dotazů měsíčně",
      "Přístup k základní právní databázi",
      "Emailová podpora",
    ],
    cta: "Začít Zdarma",
  },
  {
    name: "Pro",
    price: "499 Kč / měsíc",
    features: [
      "Neomezené dotazy",
      "Přístup k rozšířené právní databázi",
      "Prioritní emailová podpora",
      "Export dokumentů",
    ],
    cta: "Vyzkoušet Pro",
  },
  {
    name: "Enterprise",
    price: "Dle potřeb",
    features: [
      "Vše z Pro plánu",
      "Vlastní AI model",
      "Integrace s vašimi systémy",
      "Dedikovaná zákaznická podpora",
    ],
    cta: "Kontaktujte Nás",
  },
];

export default function Pricing({ id }: pricingProps) {
  return (
    <section
      id={id}
      className=" flex flex-col gap-16 py-16 px-4 sm:px-6 lg:px-8 bg-muted"
    >
      <h2 className="text-3xl font-bold text-center text-foreground">
        Cenové Plány
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
        {plans.map((plan, index) => {
          const isFree = plan.name === "Základní";
          return (
            <div
              key={index}
              className={
                !isFree
                  ? "bg-background p-8 rounded-lg shadow-md flex flex-col opacity-50 pointer-events-none"
                  : "bg-background p-8 rounded-lg shadow-md flex flex-col"
              }
            >
              <h3 className="text-xl font-semibold mb-4">{plan.name}</h3>
              <p className="text-3xl font-bold mb-6">{plan.price}</p>
              <ul
                className={`space-y-2 mb-8 flex-grow ${
                  isFree ? "opacity-50 pointer-events-none" : ""
                }`}
              >
                {plan.features.map((feature, featureIndex) => (
                  <li key={featureIndex} className="flex items-center">
                    <Check className="h-5 w-5 text-green-500 mr-2" />
                    <span>{feature}</span>
                  </li>
                ))}
              </ul>
              {isFree && (
                <p className="text-sm text-red-500 mb-4">
                  Prozatím je dostupná aplikace s veškerou její funkcionalitou
                  zdarma pro všechny uživatele, protože je stále v beta verzi.
                </p>
              )}
              <Button className="w-full">{plan.cta}</Button>
            </div>
          );
        })}
      </div>
    </section>
  );
}
