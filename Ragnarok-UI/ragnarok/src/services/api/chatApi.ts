import { ChatWebSocket } from "../websocket";

export const chatApi = async (ws: ChatWebSocket,message: string): Promise<{ response: string }> => {

  return new Promise((resolve, reject) => {
    try {
      ws.onMessage((data) => {
        if (data) {
          resolve({ response: data });
        } else {
          reject(new Error("Invalid response format from server"));
        }
      });

      ws.sendMessage(message);
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
