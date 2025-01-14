import React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const testimonials = [
  {
    name: "Jana Nováková",
    role: "Majitelka Malého Podniku",
    content:
      "Tento nástroj změnil hru pro moje podnikání. Mohu rychle kontrolovat předpisy bez drahých právních konzultací.",
    avatar: "/placeholder.svg?height=40&width=40",
  },
  {
    name: "Petr Svoboda",
    role: "Občan",
    content:
      "Nikdy jsem si nemyslel, že porozumění právu může být tak snadné. Je to jako mít právního experta v kapse!",
    avatar: "/placeholder.svg?height=40&width=40",
  },
  {
    name: "Martina Dvořáková",
    role: "Advokátka",
    content:
      "Jako právní profesionál jsem ohromená přesností a hloubkou informací. Je to neocenitelný zdroj pro rychlé reference.",
    avatar: "/placeholder.svg?height=40&width=40",
  },
];

interface TestimonialsProps {
  id?: string;
}

export default function Testimonials({ id }: TestimonialsProps) {
  return (
    <section id={id} className="py-20 px-4 sm:px-6 lg:px-8 bg-gray-50">
      <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">
        Co Říkají Naši Uživatelé
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
        {testimonials.map((testimonial, index) => (
          <Card key={index} className="shadow-xl shadow-secondary">
            <CardContent className="p-6">
              <p className="text-gray-600 mb-4">
                &quot;{testimonial.content}&quot;
              </p>
              <div className="flex items-center">
                <Avatar className="h-10 w-10 mr-4">
                  <AvatarImage
                    src={testimonial.avatar}
                    alt={testimonial.name}
                  />
                  <AvatarFallback>{testimonial.name[0]}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-semibold">{testimonial.name}</p>
                  <p className="text-sm text-gray-500">{testimonial.role}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}
