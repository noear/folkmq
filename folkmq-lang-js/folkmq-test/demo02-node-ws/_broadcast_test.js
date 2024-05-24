const {FolkMQ} = require("@noear/folkmq");
const {MqMessage} = require("@noear/folkmq/client/MqMessage");

async function main() {
    const client1 =  await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("demoapp")
        .connect();

    const client2 =  await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("demoapp")
        .connect();

    //订阅
    client1.subscribe("test", null, true, message => {
        console.log(message.getBodyAsString());
    });

    client2.subscribe("test", null, true, message => {
        console.log(message.getBodyAsString());
    });

    //发布
    client1.publish("test", new MqMessage("hot-1").broadcast(false));
    client1.publish("test", new MqMessage("hot-2").broadcast(true));
}

main();