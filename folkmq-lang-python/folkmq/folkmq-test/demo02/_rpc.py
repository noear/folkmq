import asyncio

from socketd.transport.core.entity.StringEntity import StringEntity
from socketd.utils.LogConfig import log

from folkmq.FolkMQ import FolkMQ
from folkmq.client.MqMessage import MqMessage
from folkmq.client.MqRouter import MqRouter


async def main():
    # 客户端1
    client1 = await (FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
                    .nameAs("app1")
                    .connect())

    router = MqRouter(lambda m: m.getTag()).doOn("/test/hello", lambda req:
        req.response(StringEntity("me to!"))
    )
    #客户端1监听
    await client1.listen(router.consume)

    # 客户端2
    client2 = await (FolkMQ.createClient("folkmq:ws://127.0.0.1:18602")
                     .nameAs("app2")
                     .connect())

    rep = await client2.send(MqMessage("hello").tag("/test/hello"), "app1").waiter()
    log.info(":::" + rep.data_as_string());

    await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())

