import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Progress } from "@/components/ui/progress";
import { Separator } from "../ui/separator";

type OnboardingStep = {
  title: string;
  description: string;
};

const onboardingSteps: OnboardingStep[] = [
  {
    title: `Vítejte`,
    description:
      "Pojďme si projít rychlý úvod, jak používat naši aplikaci a získat co nejvíce z jejích funkcí!",
  },
  {
    title: "Položení dotazu",
    description:
      "Zadejte své právní otázky do chatovacího okna nebo si vyberte z běžných ukázkových otázek a začněte.",
  },
  {
    title: "Odpovědi s podporou AI",
    description:
      "Odpovědi jsou vypracovány umělou inteligencí na základě českých právních dokumentů.",
  },
  {
    title: "Hodnocení odpovědí",
    description:
      "Pomozte nám zlepšovat se hodnocením užitečnosti odpovědí AI pomocí tlačítek palec nahoru nebo dolů.",
  },
  {
    title: "Uložení konverzace",
    description:
      "Před opuštěním stránky si nezapomeňte uložit konverzace, které si přejete zachovat v poměti vašeho prohlížeče.",
  },
];

export function OnboardingModal({
  isOpen,
  onClose,
}: {
  isOpen: boolean;
  onClose: () => void;
}) {
  const [currentStep, setCurrentStep] = useState(0);

  const handleNext = () => {
    if (currentStep < onboardingSteps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      onClose();
    }
  };

  const progress = ((currentStep + 1) / onboardingSteps.length) * 100;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="w-[500px] sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle className="text-3xl">
            {onboardingSteps[currentStep].title}
            {currentStep === 0 ? <span className="animate-wave">👋</span> : ""}
          </DialogTitle>
          <Separator className="mt-2" />
          <DialogDescription className="mt-2">
            {onboardingSteps[currentStep].description}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="flex flex-col items-center pt-4">
          <div className="flex flex-col items-center w-full">
            <Button onClick={handleNext} className="w-full border-none">
              {currentStep < onboardingSteps.length - 1
                ? "Další"
                : "Pojďme na to!"}
            </Button>
            <Progress value={progress} className="mt-3 h-1.5" />
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
