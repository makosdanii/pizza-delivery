import {Stomp} from "@stomp/stompjs";
import SockJS from 'sockjs-client';
import server from '@/business/PizzaServerAPI.js'

const SERVER_URL = "http://localhost:8081";

let stomp = null
export default {
    connect: (callbackFn) => {
        const socket = new SockJS(`${SERVER_URL}/delivery`);
        stomp = Stomp.over(socket);
        stomp.connect({}, function (frame) {
            stomp.subscribe(`/delivery/${server.id()}`, function (messageOutput) {
                callbackFn(messageOutput)
            })
        })
    },
    disconnect: () => {
        if (stomp !== null) {
            stomp.disconnect(() =>
                console.log("Client disconnected")
            )
            stomp = null
        }
    }
}