const {FolkMQ} = require("@noear/folkmq");

async function main() {
    const client =  await FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("demoapp")
        .connect();

    //订阅
    client.subscribe("test", null, true, message => {
        console.log(message.getBodyAsString());
    });

    //发布
    for (let i = 0; i < 10; i++) {
        client.publish("test", FolkMQ.newMessage("hot-" + i));
    }
}

main();