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
