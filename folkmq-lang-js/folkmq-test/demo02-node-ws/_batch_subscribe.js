const {FolkMQ} = require("@noear/folkmq");

async function main() {
    const client =  FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
        .nameAs("demoapp");

    //订阅
    client.subscribe("test", null, true, message => {
        console.log(message.getBodyAsString());
    });

    client.subscribe("test2", null, true, message => {
        console.log(message.getBodyAsString());
    });

    client.subscribe("test3", null, true, message => {
        console.log(message.getBodyAsString());
    });

    //连接
    await client.connect();

    //发布
    for (let i = 0; i < 10; i++) {
        client.publish("test", FolkMQ.newMessage("test-" + i));
        client.publish("test2", FolkMQ.newMessage("test2-" + i));
    }
}

main();