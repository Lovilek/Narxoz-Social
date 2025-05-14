import { useParams } from "react-router-dom";
import ChatRoom from "../components/chat/ChatRoom";

const DirectChat = () => {
  const { chatId } = useParams();
  console.log("chatId:", chatId); // обязательно!
  return <ChatRoom chatId={chatId} />;
};

export default DirectChat;
