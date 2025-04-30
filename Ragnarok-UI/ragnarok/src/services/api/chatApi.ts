import { ChatWebSocket } from "../websocket";

export type FlowType = 'KEYWORDS' | 'CLASSIC' | 'PARAPHRASE';
export interface MessageDto {
  question: string;
  conversationId: string;
  flowType: FlowType;
}

export interface AnswerDto {
  answer: string;
  paragraphs: string;
  flow: FlowType;
}

export const chatApi = async (ws: ChatWebSocket,message: MessageDto): Promise<{ response: string }> => {

  return new Promise((resolve, reject) => {
    try {
      ws.onMessage((data) => {
        if (data) {
          resolve({ response: data });
        } else {
          reject(new Error("Invalid response format from server"));
        }
      });

      ws.sendMessage(JSON.stringify(message));
    } catch (error) {
      reject(error);
    }
  });
};

export const mockChatApi = async (
  message: string
): Promise<{ response: string }> => {
  return new Promise((resolve) =>
    setTimeout(
      () => resolve({ response: `Mocked response to: "${message}"` }),
      500
    )
  );
};
